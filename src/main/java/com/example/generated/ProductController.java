package com.example.generated;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
public class ProductController {
  @Autowired
  private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
  public List<Product> getAll() {
    return service.findAll();
  }
}
