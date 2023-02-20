package com.myomi.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.myomi.point.entity.Point;
import com.myomi.point.entity.PointDetail;
import com.myomi.point.entity.PointDetailEmbedded;
import com.myomi.point.repository.PointDetailRepository;
import com.myomi.point.repository.PointRepository;
import com.myomi.user.entity.User;
import com.myomi.user.repository.UserRepository;

@SpringBootTest
class PointRepoTest {
	private Logger logger = LoggerFactory.getLogger(getClass()); 
	@Autowired
	private PointRepository pr;
	
	@Autowired
	private PointDetailRepository pdr;
	
	@Autowired
	private UserRepository ur;
	
	@Test
	void testSavePoint() {
		Optional<User> optU = ur.findById("id7");
		Point point = optU.get().getPoint(); //토탈 포인트
		
		PointDetail pd = new PointDetail();
		PointDetailEmbedded pde = new PointDetailEmbedded();
		pde.setUId(optU.get().getId());
		LocalDateTime date = LocalDateTime.now();
		pde.setCreatedDate(date);
		pd.setPointEmbedded(pde);
		pd.setSort(2);
		pd.setAmount(148);
		Integer amt = pd.getAmount();
		point.setTotalPoint(point.getTotalPoint()+amt);
		point.setUserId(optU.get());
		pd.setPoint(point);
		
		pr.save(point);
		pdr.save(pd);
	}

	
	@Test
	void testFindPointAll() {
		Optional<User> optU = ur.findById("id7");
		Iterable<PointDetail> pds= pdr.findAll();
		pds.forEach((pd)->{
			logger.info("[" + optU.get().getName()+"]님의 포인트 적립 내역: " );
			if(pd.getSort() == 0 ) {
				logger.info(pd.getPointEmbedded().getCreatedDate() + "| 회원가입 축하 [" + pd.getAmount() + "]원 적립");
			} else if(pd.getSort() == 1) {
				logger.info(pd.getPointEmbedded().getCreatedDate() + "| 상품 구매 [" + pd.getAmount() + "]원 적립");
			} else {
				logger.info(pd.getPointEmbedded().getCreatedDate() + "| 그 외 [" + pd.getAmount() + "]원 적립");
			}
			logger.info("--------------------------------------------------" );
		});
	}
}
