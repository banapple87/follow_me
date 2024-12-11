import React from "react";
import '../styles/EtcSearch.css'

const EtcSearch = () => {
  const containerStyle = {
    backgroundImage: `url(${process.env.PUBLIC_URL}/background.png)`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundRepeat: 'no-repeat',
    height: '100vh',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    textAlign: 'center',
    padding: '20px'
  };

  const handleSearchClick = () => {
    alert("검색 버튼이 클릭되었습니다.");
  };


  const handleCategoryClick = (category) => {
    alert(`${category} 카테고리가 선택되었습니다.`);
  };

  return (
    <div style={containerStyle} className="container">
      <h1>기타시설 검색</h1>
      <div className="separator"></div>

    
      <div className="search-input-container">
        <input
          type="text"
          className="search-input"
          placeholder="검색어를 입력하시오"
        />
        <button
          type="button"
          className="search-button"
          onClick={handleSearchClick}
        >
          검색
        </button>
      </div>

      

      
      <h2>자주 찾는 검색어</h2>
      <div className="button-group">
        <button
          type="button"
          className="add-button"
          onClick={() => handleCategoryClick("편의점")}
        >
          #편의점
        </button>
        <button
          type="button"
          className="add-button"
          onClick={() => handleCategoryClick("수유실")}
        >
          #수유실
        </button>
        <button
          type="button"
          className="add-button"
          onClick={() => handleCategoryClick("화장실")}
        >
          #화장실
        </button>
        <button
          type="button"
          className="add-button"
          onClick={() => handleCategoryClick("흡연실")}
        >
          #흡연실
        </button>
      </div>
    </div>
  );
};

export default EtcSearch;



  