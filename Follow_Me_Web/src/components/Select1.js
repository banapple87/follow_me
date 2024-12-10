import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Select1.css';

const Select1 = () => {
  const navigate = useNavigate();
  
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

  const handleShopClick = () => {
    navigate('/select/select_info');
  };

  const handleSearchClick = () => {
    navigate('/select/etcsearch'); 
  };

  return (
    <div style={containerStyle} className="container">
      <img src = {`${process.env.PUBLIC_URL}/main_logo.png`} alt="logo" className="main-logo" />
      
      <div className='button'>
        <img 
          src={`${process.env.PUBLIC_URL}/etc_icon.png`} 
          alt="기타시설 검색 버튼" 
          className="search-button" 
          onClick={handleSearchClick} 
        />
        <img 
          src={`${process.env.PUBLIC_URL}/shop_icon.png`} 
          alt="쇼핑 버튼" 
          className="shopping-button" 
          onClick={handleShopClick} 
        />
      </div>
    </div>
  );
};

export default Select1;
