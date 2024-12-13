import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import styles from "../styles/Review.module.css";

const Review = () => {
  const navigate = useNavigate();
  const [reviews, setReviews] = useState([]); 
  const [searchValue, setSearchValue] = useState("");
  const [selectedPlace, setSelectedPlace] = useState("");

  // 리뷰 데이터 가져오기
  useEffect(() => {
    const fetchReviews = async () => {
      try {
        const response = await axios.get("http://localhost:3000/api/reviews");
        console.log(response.data);
        setReviews(response.data);
      } catch (error) {
        console.error("리뷰 데이터를 가져오는 중 오류:", error);
      }
    };
    fetchReviews();
  }, []);

  // 검색 이벤트 핸들러
  const handleSearchClick = () => {
    if (!searchValue.trim()) {
      alert("검색어를 입력하세요.");
      return;
    }
    setSelectedPlace(searchValue.trim());
  };

  // 버튼 클릭 이벤트 핸들러
  const handleCategoryClick = (category) => {
    setSelectedPlace(category);
  };

  const handleGoHome = () => {
    navigate("/");
  };

  const containerStyle = {
    backgroundImage: `url(${process.env.PUBLIC_URL}/background.png)`,
    backgroundSize: "cover",
    backgroundPosition: "center",
    backgroundRepeat: "no-repeat",
    height: "100vh",
  };

  // 선택된 식당의 리뷰 요약 필터링
  const filteredReview = reviews.find(
    (review) => review.place_name === selectedPlace
  );

  return (
    <div style={containerStyle} className={styles.container}>
      {/* 왼쪽 섹션: 제목, 검색창, 리뷰 요약 */}
      <div className={styles.left_Section}>
        <h1 className={styles.header_Title}>식당가 검색</h1>
        
        {/* 검색창 */}
        <div className={styles.find_Input_Container}>
          <input
            type="text"
            className={styles.find_Input}
            placeholder="검색어를 입력하세요."
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
          />
          <button
            type="button"
            className={styles.find_Button}
            onClick={handleSearchClick}
          >
            🔍︎
          </button>
        </div>

        {/* 리뷰 요약 섹션 */}
        <div className={styles.review_Summary_Container}>
          <h2>리뷰 요약</h2>
          {selectedPlace ? (
            filteredReview ? (
              <div className={styles.review_Card}>
                <h3>{filteredReview.place_name}</h3>
                <p>{filteredReview.summary.split('\n\n').map((line, index) => (
                    <React.Fragment key={index}>
                      {line}
                      <br />
                    </React.Fragment>
                  ))}
                </p>
              </div>
            ) : (
              <p>선택된 식당의 리뷰 요약을 찾을 수 없습니다.</p>
            )
          ) : (
            <p>식당을 선택하거나 검색하세요.</p>
          )}
        </div>
      </div>

      {/* 오른쪽 섹션: 버튼 그룹 */}
      <div className={styles.right_Section}>
        <div className={styles.button_Group}>
          {[
            "개미집", "풍원장시골밥상", "시연솥밥", "제주항갈치고등어쌈정식",
            "매드포갈릭", "아웃백스테이크하우스", "애슐리퀸즈",
            "고동경양", "미스카츠", "무한계도", "겐츠베이커리",
            "폴바셋", "온기정", "동경규동", "우츄진",
            "스시츠카무", "한양중식", "띤띤", "솔솥",
            "구이구이", "이가네떡볶이", "할매솥충무김밥", "수훈식당",
            "고피자"
          ].map((name) => (
            <button
              key={name}
              type="button"
              className={styles.add_Button}
              onClick={() => handleCategoryClick(name)}
            >
              {name}
            </button>
          ))}
        </div>
      </div>
      <button className="home-button" onClick={handleGoHome}>
        Home
      </button>
    </div>
  );
};

export default Review;
