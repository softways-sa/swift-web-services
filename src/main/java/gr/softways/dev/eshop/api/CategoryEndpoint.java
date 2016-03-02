package gr.softways.dev.eshop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.softways.dev.jdbc.Database;
import gr.softways.dev.jdbc.Load;
import gr.softways.dev.jdbc.MetaDataUpdate;
import gr.softways.dev.jdbc.QueryDataSet;
import gr.softways.dev.jdbc.QueryDescriptor;
import gr.softways.dev.util.DbRet;
import gr.softways.dev.util.Director;
import gr.softways.dev.util.SwissKnife;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Panos
 */
public class CategoryEndpoint extends HttpServlet {
  private Director director;
  
  private String databaseId;
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    databaseId = SwissKnife.jndiLookup("swconf/databaseId");
    
    director = Director.getInstance();
  }
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setCharacterEncoding("utf-8");
    
    findCategoriesById(request, response);
  }
  
  private void findCategoriesById(HttpServletRequest request, HttpServletResponse response) {
    response.setContentType("application/json; charset=UTF-8");
    
    PrintWriter out;
    
    DbRet dbRet;
    
    QueryDataSet queryDataSet;
    
    Map<String, Object> result = new HashMap<String, Object>();
    
    List resultList = new ArrayList<PrdCategoryDTO>();
    
    ObjectMapper mapper = new ObjectMapper();
    
    StringBuilder s = new StringBuilder();
    
    String id = request.getParameter("id") == null ? "" : request.getParameter("id");
    
    StringBuilder query = new StringBuilder();
    
    StringBuilder subCatId = new StringBuilder(id + "__");
    
    for (int i=0; i<25-subCatId.length(); i++) subCatId.append(" ");
    
    query.append("SELECT catId,catParentFlag,catName,catImgName1");
    query.append(" FROM prdCategory WHERE catId LIKE '").append(SwissKnife.sqlEncode(subCatId.toString())).append("%'");
    query.append(" AND catShowFlag = '1' ORDER BY catId");
    
    Database database = director.getDBConnection(databaseId);

    dbRet = database.beginTransaction(Database.TRANS_ISO_SIMPLE);

    int prevTransIsolation = dbRet.getRetInt();
    
    try {
      queryDataSet = new QueryDataSet();
      
      queryDataSet.setQuery(new QueryDescriptor(database,query.toString(),null,true,Load.ALL));
      
      queryDataSet.setMetaDataUpdate(MetaDataUpdate.NONE);

      queryDataSet.refresh();

      boolean isFirstCategory = true;
          
      while (queryDataSet.inBounds()) {
        PrdCategoryDTO prdCategory = new PrdCategoryDTO();
        
        prdCategory.setId(queryDataSet.getString("catId"));
        prdCategory.setName(SwissKnife.sqlDecode(queryDataSet.getString("catName")));
        if (queryDataSet.getString("catImgName1").length() > 0) {
          prdCategory.setImage("http://" + request.getServerName() + "/images/" + queryDataSet.getString("catImgName1"));
        }
        else {
          prdCategory.setImage("http://" + request.getServerName() + "/images/prd_cat_not_avail.png");
        }
        
        if (isFirstCategory && id.length() > 0) {
          result.put("parent", prdCategory);
          isFirstCategory = false;
        }
        else {
          resultList.add(prdCategory);
        }
        
        queryDataSet.next();
      }
    }
    catch (Exception e) {
      dbRet.setNoError(0);
      e.printStackTrace();
    }

    result.put("categories", resultList);
    
    database.commitTransaction(dbRet.getNoError(), prevTransIsolation);

    director.freeDBConnection(databaseId, database);
    
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