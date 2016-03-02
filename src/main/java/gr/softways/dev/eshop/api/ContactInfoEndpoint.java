package gr.softways.dev.eshop.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.softways.dev.util.Configuration;
import gr.softways.dev.util.SwissKnife;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
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
public class ContactInfoEndpoint extends HttpServlet {
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
    
    findContactInfo(request, response);
  }
  
  private void findContactInfo(HttpServletRequest request, HttpServletResponse response) {
    response.setContentType("application/json; charset=UTF-8");
    
    PrintWriter out;
    
    Map<String, Object> result = new HashMap<String, Object>();
    
    ObjectMapper mapper = new ObjectMapper();
    
    ContactInfoDTO contactInfo = new ContactInfoDTO();
    
    String[] values = Configuration.getValues(new String[] {"mob-app.contact.title","mob-app.contact.email",
      "mob-app.contact.address.line1","mob-app.contact.address.line2","mob-app.contact.postal",
      "mob-app.contact.phone","mob-app.contact.lat","mob-app.contact.lng"});

    contactInfo.setTitle(values[0]);
    contactInfo.setEmail(values[1]);
    contactInfo.setAddress1(values[2]);
    contactInfo.setAddress2(values[3]);
    contactInfo.setPostalCode(values[4]);
    contactInfo.setPhone(values[5]);
    contactInfo.setLat(values[6]);
    contactInfo.setLng(values[7]);
        
    result.put("contactInfo", contactInfo);
    
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