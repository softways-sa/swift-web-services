package gr.softways.dev.eshop.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 *
 * @author Panos
 */
@JsonInclude(Include.NON_NULL)
public class ProductDTO {
  private String id;
  private String name;
  private String description;
  private String thumb;
  private String url;
  private BigDecimal price;
  private ArrayList<String> gallery = new ArrayList<String>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getThumb() {
    return thumb;
  }

  public void setThumb(String thumb) {
    this.thumb = thumb;
  }

  public ArrayList<String> getGallery() {
    return gallery;
  }

  public void addToGallery(String s) {
    gallery.add(s);
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}