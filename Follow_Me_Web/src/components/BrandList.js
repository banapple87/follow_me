import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import '../styles/BrandList.css';

function BrandList() {
  const location = useLocation();
  const navigate = useNavigate();
  const brands = location.state?.brands || [];

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

  const handleGoHome = () => {
    navigate('/');
  };

  return (
    <div style={containerStyle} className='container'>
      <h2>추천리스트가 생성되었습니다.</h2>
      <div className="separator"></div>
      <div>
        {brands.length === 0 ? (
          <p>조건에 맞는 브랜드가 없습니다.</p>
        ) : (
          <ul>
            {brands.map((brand, index) => (
              <li key={index} className="list-item">{brand} </li>
            ))}
          </ul>
        )}
      </div>
      <button className="home-button" onClick={handleGoHome}>
        Home
      </button>
    </div>
  );
}

export default BrandList;
