import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Select1.css';

const Select1 = () => {
  const navigate = useNavigate();
  
  const containerStyle = {
    backgroundImage: `url(/background.png)`,
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

  const handleShopClick = () => {
    navigate('/select/select_info');
  };

  const handleSearchClick = () => {
    navigate('/select/etcsearch'); 
  };

  const handlefoodClick = () => {
    navigate('/select/review'); 
  };
  return (
    <div style={containerStyle} className="container">
      <h1>Follow Me</h1>
      <h3>쇼핑의 즐거움을 공유하다.</h3>
      
      <div className="button-group">
        <div className="button-container" onClick={handleSearchClick}>
          <img
            src={`${process.env.PUBLIC_URL}/etc_icon.png`}
            alt="기타시설 검색 버튼"
            className="search-button"
          />
          <p className="button-text">기타시설</p>
        </div>

        <div className="button-container" onClick={handleShopClick}>
          <img
            src={`${process.env.PUBLIC_URL}/shop_icon.png`}
            alt="쇼핑 버튼"
            className="shopping-button"
          />
          <p className="button-text">쇼핑</p>
        </div>

        <div className="button-container" onClick={handlefoodClick}>
          <img
            src={`${process.env.PUBLIC_URL}/food_icon.png`}
            alt="식당 검색 버튼"
            className="food-button"
          />
          <p className="button-text">식당 검색</p>
        </div>
      </div>
    </div>
  );
};

export default Select1;
