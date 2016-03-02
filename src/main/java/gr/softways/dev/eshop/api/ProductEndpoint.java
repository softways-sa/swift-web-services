package gr.softways.dev.eshop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.softways.dev.eshop.eways.Customer;
import gr.softways.dev.eshop.eways.v2.PrdPrice;
import gr.softways.dev.eshop.eways.v5.PriceChecker;
import gr.softways.dev.eshop.product.v2.Present2_2;
import gr.softways.dev.eshop.product.v2.Search2_3;
import gr.softways.dev.util.SwissKnife;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Panos
 */
public class ProductEndpoint extends HttpServlet {
  private ServletContext servletContext;
  
  private String databaseId;
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    
    servletContext = getServletContext();
  }
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setCharacterEncoding("utf-8");
    
    String id = request.getParameter("id") == null ? "" : request.getParameter("id");
    
    if (id.length() > 0) {
      findProductById(request, response, id);
    }
    else {
      findProductsByQuery(request, response);
    }
  }
  
  private void findProductsByQuery(HttpServletRequest request, HttpServletResponse response) {
    response.setContentType("application/json; charset=UTF-8");
    
    PrintWriter out;
    
    Map<String, Object> result = new HashMap<String, Object>();
    
    List resultList = new ArrayList<ProductDTO>();
    
    ObjectMapper mapper = new ObjectMapper();
    
    Search2_3 productSearch = new Search2_3();
    productSearch.initBean(databaseId, request, response, this, null);
    productSearch.setAuthUsername("anonymous");
    productSearch.setAuthPassword("softways");
    
    int limit = 24;
    
    if (request.getParameter("limit") != null) {
      limit = Integer.parseInt(request.getParameter("limit"));
    }
    
    productSearch.setSortedByCol("prdId ASC");
    productSearch.setSortedByOrder("");
    productSearch.setDispRows(limit);
    
    productSearch.doAction(request);
    while (productSearch.inBounds()) {
      ProductDTO product = new ProductDTO();

      product.setId(productSearch.getColumn("prdId"));
      product.setUrl("http://" + request.getServerName() + "/product_detail.jsp?prdId=" + productSearch.getColumn("prdId"));
      product.setName(productSearch.getColumn("name"));
      product.setThumb("http://" + request.getServerName() + "/prd_images/" + productSearch.getColumn("prdId") + "-1.jpg");
      
      PrdPrice prdPrice = PriceChecker.calcPrd(BigDecimal.ONE,
          productSearch.getQueryDataSet(),
          Customer.CUSTOMER_TYPE_RETAIL,
          PriceChecker.isOffer(productSearch.getQueryDataSet(),Customer.CUSTOMER_TYPE_RETAIL),
          BigDecimal.ZERO);
      if (prdPrice.getUnitGrossCurr1().compareTo(BigDecimal.ZERO) == 1) {
        product.setPrice(prdPrice.getUnitGrossCurr1().setScale(2, RoundingMode.HALF_UP));
      }
      
      resultList.add(product);
      
      productSearch.nextRow();
    }
    productSearch.closeResources();
    
    result.put("products", resultList);
    
    try {
      out = response.getWriter();
      
      mapper.writeValue(out, result);
      
      out.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private void findProductById(HttpServletRequest request, HttpServletResponse response, String prdId) {
    response.setContentType("application/json; charset=UTF-8");
    
    PrintWriter out;
    
    Map<String, Object> result = new HashMap<String, Object>();
    
    ObjectMapper mapper = new ObjectMapper();
    
    ProductDTO product = new ProductDTO();

    Present2_2 productSearch = new Present2_2();
    productSearch.initBean(databaseId, request, response, this, null);
    productSearch.setAuthUsername("anonymous");
    productSearch.setAuthPassword("softways");
    productSearch.getPrd(prdId, SwissKnife.jndiLookup("swconf/inventoryType"));
    
    product.setId(productSearch.getColumn("prdId"));
    product.setUrl("http://" + request.getServerName() + "/product_detail.jsp?prdId=" + productSearch.getColumn("prdId"));
    product.setName(productSearch.getColumn("name"));
    product.setDescription(productSearch.getColumn("descr"));
    product.setThumb("http://" + request.getServerName() + "/prd_images/" + productSearch.getColumn("prdId") + "-1.jpg");

    for (int i=1; i<=8; i++) {
      if (SwissKnife.fileExists(servletContext.getRealPath("") + "/prd_images/" + productSearch.getColumn("prdId") + "-" + i + ".jpg")) {
        product.addToGallery("http://" + request.getServerName() + "/prd_images/" + productSearch.getColumn("prdId") + "-" + i + ".jpg");
      }
    }
    
    PrdPrice prdPrice = PriceChecker.calcPrd(BigDecimal.ONE,
        productSearch.getQueryDataSet(),
        Customer.CUSTOMER_TYPE_RETAIL,
        PriceChecker.isOffer(productSearch.getQueryDataSet(),Customer.CUSTOMER_TYPE_RETAIL),
        BigDecimal.ZERO);
    if (prdPrice.getUnitGrossCurr1().compareTo(BigDecimal.ZERO) == 1) {
      product.setPrice(prdPrice.getUnitGrossCurr1().setScale(2, RoundingMode.HALF_UP));
    }
    
    result.put("product", product);
    
    try {
      out = response.getWriter();
      
      mapper.writeValue(out, result);
      
      out.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}