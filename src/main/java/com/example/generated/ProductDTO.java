package com.example.generated;

import io.swagger.v3.oas.annotations.media.Schema;

public class ProductDTO {
  @Schema(
      description = "Mevalar"
  )
  private String Name;

  public String getName() {
    return Name;
  }

  public void setName(String Name) {
    this.Name = Name;
  }
}
