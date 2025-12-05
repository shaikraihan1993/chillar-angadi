package com.grocerystore.grocery_store.controller

import com.grocerystore.grocery_store.model.Product
import com.grocerystore.grocery_store.model.ProductImage
import com.grocerystore.grocery_store.repository.ProductImageRepository
import com.grocerystore.grocery_store.repository.ProductRepository
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.core.io.ByteArrayResource

@RestController
@RequestMapping("/api/v1")
class FileUploadController {

    private final ProductRepository productRepository
    private final ProductImageRepository productImageRepository

    FileUploadController(ProductRepository productRepository, ProductImageRepository productImageRepository) {
        this.productRepository = productRepository
        this.productImageRepository = productImageRepository
    }

    @PostMapping("/upload")
    ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "productId", required = false) Long productId
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body([error: "Please select a file to upload"])
            }

            if (!file.contentType?.startsWith("image/")) {
                return ResponseEntity.badRequest().body([error: "Only image files are allowed"])
            }

            // Save the image
            def newImage = productImageRepository.save(new ProductImage(
                fileName: file.originalFilename,
                fileType: file.contentType,
                data: file.bytes
            ))

            // If productId is provided, link the image to the product
            if (productId != null) {
                productRepository.findById(productId).ifPresent { product ->
                    // Remove old image if exists
                    if (product.image != null) {
                        productImageRepository.deleteById(product.image.id)
                    }
                    // Set new image
                    product.image = newImage
                    productRepository.save(product)
                }
            }

            return ResponseEntity.ok([id: newImage.id.toString()])

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body([error: "Could not upload the file: ${e.message}"])
        }
    }

    @GetMapping("/images/{id}")
    ResponseEntity<ByteArrayResource> getImage(@PathVariable("id") Long id) {
        try {
            return productImageRepository.findById(id).map { image ->
                if (image.data == null || image.data.length == 0) {
                    return ResponseEntity.notFound().build()
                }

                // Set content type
                def mediaType = MediaType.IMAGE_JPEG // default
                if (image.fileType?.toLowerCase()?.contains("png")) {
                    mediaType = MediaType.IMAGE_PNG
                } else if (image.fileType?.toLowerCase()?.contains("gif")) {
                    mediaType = MediaType.IMAGE_GIF
                }

                ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${image.fileName ?: 'image'}\"")
                    .body(new ByteArrayResource(image.data))
            }.orElse(ResponseEntity.notFound().build())

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}