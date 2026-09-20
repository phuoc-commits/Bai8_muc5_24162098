package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.ProductRepository;

import java.util.List;
import java.util.Map;

@Controller
public class GraphQLController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // --- HOME PAGE QUERIES ---
    @QueryMapping
    public List<Product> productsSortedByPriceAsc() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "price"));
    }

    @QueryMapping
    public List<Product> productsByCategoryId(@Argument Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    // --- SEARCH & PAGINATION QUERIES ---
    @QueryMapping
        public ProductPageResponse searchProducts(@Argument String keyword, @Argument Integer page, @Argument Integer size) {
        int currentPage = page == null ? 0 : page;
        int pageSize = size == null ? 5 : size;
        validatePage(currentPage, pageSize);
        Page<Product> productPage = productRepository.findByTitleContainingIgnoreCase(
            keyword == null ? "" : keyword, PageRequest.of(currentPage, pageSize));
        return new ProductPageResponse(productPage.getContent(), productPage.getTotalPages(),
            productPage.getTotalElements(), currentPage);
    }

    @QueryMapping
        public CategoryPageResponse searchCategories(@Argument String keyword, @Argument Integer page, @Argument Integer size) {
        int currentPage = page == null ? 0 : page;
        int pageSize = size == null ? 5 : size;
        validatePage(currentPage, pageSize);
        Page<Category> categoryPage = categoryRepository.findByNameContainingIgnoreCase(
            keyword == null ? "" : keyword, PageRequest.of(currentPage, pageSize));
        return new CategoryPageResponse(categoryPage.getContent(), categoryPage.getTotalPages(),
            categoryPage.getTotalElements(), currentPage);
    }

    // --- PRODUCT MUTATIONS ---
    @MutationMapping
    public Product createProduct(@Argument Map<String, Object> input) {
        Product product = new Product();
        return saveOrUpdateProduct(product, input);
    }

    @MutationMapping
    public Product updateProduct(@Argument Map<String, Object> input) {
        Long id = Long.parseLong(input.get("id").toString());
        Product product = productRepository.findById(id).orElseThrow();
        return saveOrUpdateProduct(product, input);
    }

    @MutationMapping
    public Boolean deleteProduct(@Argument Long id) {
        if (!productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
        return true;
    }

    private Product saveOrUpdateProduct(Product product, Map<String, Object> input) {
        product.setTitle((String) input.get("title"));
        product.setPrice(Double.parseDouble(input.get("price").toString()));
        product.setQuantity(Integer.parseInt(input.get("quantity").toString()));
        product.setDescription((String) input.get("description"));
        product.setImages((String) input.get("images"));
        
        Long categoryId = Long.parseLong(input.get("categoryId").toString());
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        product.setCategory(category);

        return productRepository.save(product);
    }

    // --- CATEGORY MUTATIONS ---
    @MutationMapping
    public Category createCategory(@Argument Map<String, Object> input) {
        Category category = new Category();
        category.setName((String) input.get("name"));
        return categoryRepository.save(category);
    }

    @MutationMapping
    public Category updateCategory(@Argument Map<String, Object> input) {
        Long id = Long.parseLong(input.get("id").toString());
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setName((String) input.get("name"));
        return categoryRepository.save(category);
    }

    @MutationMapping
    public Boolean deleteCategory(@Argument Long id) {
        if (!categoryRepository.existsById(id)) {
            return false;
        }
        categoryRepository.deleteById(id);
        return true;
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("page must be >= 0 and size must be > 0");
        }
    }

    public static class ProductPageResponse {
        private final List<Product> content;
        private final int totalPages;
        private final long totalElements;
        private final int currentPage;

        public ProductPageResponse(List<Product> content, int totalPages, long totalElements, int currentPage) {
            this.content = content;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
            this.currentPage = currentPage;
        }

        public List<Product> getContent() { return content; }
        public int getTotalPages() { return totalPages; }
        public long getTotalElements() { return totalElements; }
        public int getCurrentPage() { return currentPage; }
    }

    public static class CategoryPageResponse {
        private final List<Category> content;
        private final int totalPages;
        private final long totalElements;
        private final int currentPage;

        public CategoryPageResponse(List<Category> content, int totalPages, long totalElements, int currentPage) {
            this.content = content;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
            this.currentPage = currentPage;
        }

        public List<Category> getContent() { return content; }
        public int getTotalPages() { return totalPages; }
        public long getTotalElements() { return totalElements; }
        public int getCurrentPage() { return currentPage; }
    }
}