import React from "react";
import { useNavigate } from 'react-router-dom';
import styles from "../styles/Review.module.css"; // CSS 모듈 불러오기

const EtcSearch = () => {
  const navigate = useNavigate();
  const handleGoHome = () => {
    navigate('/');
  };

  const containerStyle = {
    backgroundImage: `url(${process.env.PUBLIC_URL}/background.png)`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundRepeat: 'no-repeat',
    height: '100vh',
  };

  const handleSearchClick = () => {
    alert("검색 버튼이 클릭되었습니다.");
  };

  const handleCategoryClick = (category) => {
    alert(`${category} 카테고리가 선택되었습니다.`);
  };

  return (
    <div style={containerStyle} className={styles.container}>
      {/* 왼쪽 섹션: 제목과 검색창 */}
      <div className={styles.left_Section}>
        <h1 className={styles.header_Title}>식당가 검색</h1>
        <div className={styles.find_Input_Container}>
          <input
            type="text"
            className={styles.find_Input}
            placeholder="검색어를 입력하세요."
          />
          <button
            type="button"
            className={styles.find_Button}
            onClick={handleSearchClick}
          >
            🔍︎
          </button>
        </div>
      </div>

      {/* 오른쪽 섹션: 버튼 그룹 */}
      <div className={styles.right_Section}>
        <div className={styles.button_Group}>
          {[
            "개미집", "풍원장 시골밥상", "시연솥밥", "제주항갈치 고등어쌈정식",
            "매드포갈릭", "아웃백스테이크하우스", "애슐리퀸즈",
            "고동경양", "미스카츠", "무한계도", "겐츠베이커리",
            "폴바셋", "온기정", "동경규동", "우츄진",
            "스시츠카무", "한양중식", "띤띤", "솥솥",
            "구이구이", "이가네떡볶이", "할매솥 충무김밥", "수훈식당",
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

export default EtcSearch;
