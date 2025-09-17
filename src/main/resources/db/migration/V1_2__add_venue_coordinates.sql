-- 청첩장 테이블에 좌표 필드 추가
-- 기존 데이터는 NULL로 유지하고, 이후 사용자가 주소 검색을 통해 좌표를 업데이트할 수 있도록 함

ALTER TABLE marry1q_invitation 
ADD COLUMN venue_latitude DECIMAL(10, 8) NULL COMMENT '결혼식장 위도',
ADD COLUMN venue_longitude DECIMAL(11, 8) NULL COMMENT '결혼식장 경도';

