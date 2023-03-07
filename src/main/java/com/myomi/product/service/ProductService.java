package com.myomi.product.service;

import com.myomi.product.dto.ProductDto;
import com.myomi.product.dto.ProductReadOneDto;
import com.myomi.product.dto.ProductSaveDto;
import com.myomi.product.dto.ProductUpdateDto;
import com.myomi.product.entity.Product;
import com.myomi.product.repository.ProductRepository;
import com.myomi.qna.dto.QnaPReadResponseDto;
import com.myomi.qna.entity.Qna;
import com.myomi.qna.repository.QnaRepository;
import com.myomi.review.dto.ReviewReadResponseDto;
import com.myomi.review.entity.Review;
import com.myomi.review.repository.ReviewRepository;
import com.myomi.s3.FileUtils;
import com.myomi.s3.S3Uploader;
import com.myomi.seller.entity.Seller;
import com.myomi.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
	@Autowired
	private S3Uploader s3Uploader;
	private final ProductRepository productRepository;
	private final SellerRepository sellerRepository;
	private final ReviewRepository reviewRepository;
	private final QnaRepository qnaRepository;
	/**
	 * TODO:
	 * 1. 셀러 등록 후 상품 등록하기
	 * 2. 등록된 상품 리스트 조회
	 * 3. 셀러별 상품 리스트 조회
	 * 4. 상품 상세 조회
	 * 5. 상품 수정
	 * 6. 상품 삭제
	 * @throws IOException 
	 */
	
	@Transactional
	public ResponseEntity<ProductSaveDto> addProduct(ProductSaveDto productSaveDto, Authentication seller) throws IOException {
		Seller s = sellerRepository.findById(seller.getName())
				.orElseThrow(() -> new IllegalArgumentException("판매자만 상품 등록이 가능합니다"));
		
		if(s.getStatus() == 3) {
			log.error("탈퇴를 신청한 판매자입니다.");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST); 
		}
		
		MultipartFile file = productSaveDto.getFile();
		
		
		if(file != null) {
			InputStream inputStream = file.getInputStream();
			
			boolean isValid = FileUtils.validImgFile(inputStream);
			if(!isValid) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}
//		else {
		String fileUrl = s3Uploader.upload(file, "상품이미지", seller, productSaveDto);
			
//			product.addProductImgUrl(fileUrl); //이거 안됨
			
//		}
		Product product = productSaveDto.toEntity(productSaveDto, s, fileUrl);
		
		//상품등록
		productRepository.save(product);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@Transactional
	public List<ProductDto> getProductBySellerId(String sellerId) {
		List<Product> prods = productRepository.findAllBySellerId(sellerId);
		List<ProductDto> list = new ArrayList<>();
		if (prods.size() == 0) {
		        log.info("등록된 상품이 없습니다.");
		} else {
		    for (Product p : prods) {
		    	ProductDto dto = new ProductDto();
		        list.add(dto.toDto(p));
		    }
		}
		return list;
	}
	
	
	@Transactional
	public ResponseEntity<?> getOneProd(Long prodNum) {
		Optional<Product> product = productRepository.findProdInfo(prodNum);
//		List<Qna> qnas = product.get().getQnas(); //이방법이 훨씬 좋아
		List<Qna> qnas = qnaRepository.findByProdNumOrderByQnaNumDesc(product.get());
		//리뷰 DTO 받으면 태리님 방식처럼 해보기 
		List<Review> reviews = reviewRepository.findAllReviewByProd(prodNum);
		
		if(product.get().getQnas().size() == 0) {	
			log.info("상품관련 문의가 없습니다.");
		}
		
		if(reviews.size() == 0) {	
			log.info("상품에 대한리뷰가 없습니다.");
		}
		
		List<QnaPReadResponseDto> qDto = new ArrayList<>();
		for(Qna q : qnas) {
			QnaPReadResponseDto qnaDto = new QnaPReadResponseDto();
			qDto.add(qnaDto.toDto(q));
		}
		
		List<ReviewReadResponseDto> rDto = new ArrayList<>();
		for(Review r : reviews) {
			ReviewReadResponseDto reviewDto = new ReviewReadResponseDto();
			rDto.add(reviewDto.toDto(r));
		}
		ProductReadOneDto dto = new ProductReadOneDto();
		return new ResponseEntity<>(dto.toDto(product.get(), rDto, qDto) , HttpStatus.OK);
	}
	
	@Transactional //성공
	public ResponseEntity<ProductUpdateDto> modifyProduct(Long prodNum, ProductUpdateDto productUpdateDto, Authentication seller) {
		Optional<Product> p = productRepository.findBySellerIdAndProdNum(seller.getName(), prodNum);
//				.orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));
		
//		Product prod = productUpdateDto.toEntity(prodNum, productUpdateDto, p.get().getSeller());
		
		if(p != null) {
			p.get().update(productUpdateDto.getDetail(), productUpdateDto.getStatus());
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@Transactional //성공
	public ResponseEntity<?> removeProduct(Long prodNum, Authentication seller) {
		Optional<Product> p = productRepository.findById(prodNum);
		Optional<Seller> s = sellerRepository.findById(seller.getName());
		if(p.get().getSeller().getId() == s.get().getId()) {
			productRepository.deleteById(prodNum);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	//상품 모든 리스트 뿌려주기. 단, status가 1인걸 기본으로 뿌려줌. <- 프론트에서 설정해야함.
//	@Transactional
//	public ResponseEntity<?> getAllProduct(int status) { 
//		List<Product> pList = productRepository.findAllByStatusOrderByWeek(status);
//		List<ProductDto> list = new ArrayList<>();
//		for(Product p : pList) {
//			ProductDto dto = new ProductDto();
//	        list.add(dto.toDto(p));
//		}
//		return new ResponseEntity<>(list, HttpStatus.OK);
//	}
	@Transactional
	public ResponseEntity<?> getAllProduct() { 
		List<Product> pList = productRepository.findAll();
		List<ProductDto> list = new ArrayList<>();
		for(Product p : pList) {
			ProductDto dto = new ProductDto();
	        list.add(dto.toDto(p));
		}
		return new ResponseEntity<>(list, HttpStatus.OK);
	}
	
	@Transactional
	public ResponseEntity<?> getAllProduct(String keyword) {
		List<Product> pList = productRepository.findAllByNameContaining(keyword);
		List<ProductDto> list = new ArrayList<>();
		if(pList.size() == 0) {
			log.error("해당 키워드가 포함된 상품이 없습니다.");
		}
		for(Product p : pList) {
			ProductDto dto = new ProductDto();
			list.add(dto.toDto(p));
		}
		return new ResponseEntity<>(list, HttpStatus.OK);
	}
	
	// 셀러페이지
	@Transactional
	public ResponseEntity<?> getOneProdBySeller(Long prodNum, Authentication seller) {
		Optional<Product> product = productRepository.findBySellerIdAndProdNum(seller.getName(),prodNum);

		ProductDto dto = new ProductDto();
		
		return new ResponseEntity<>(dto.toDto(product.get()) , HttpStatus.OK);
	}
}
