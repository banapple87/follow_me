import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Select1.css'

const Select1 = () => {
  const navigate = useNavigate();
  
  const handleShopClick = () => {
    navigate('/select/select_info');
  };

  const handleSearchClick = () => {
    alert('현재 이 기능은 사용할 수 없습니다.'); 
  };

  return (
    <div className="container">
      <h1>Follow Me</h1>
      <h2>쇼핑의 즐거움을 공유하다.</h2>
      <h3>프리미엄 아울렛 동부산점</h3>
      
      <div className='button'>
      <button
        type="button"
        className="shopping-button"
        onClick={handleShopClick}
      >
        쇼핑
      </button>
      <button
        type="button"
        className="search-button"
        onClick={handleSearchClick}
      >
        기타시설 검색
      </button>
      </div>
      
    </div>
  );
};

export default Select1;
