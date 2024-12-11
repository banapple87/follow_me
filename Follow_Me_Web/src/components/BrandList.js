import React from 'react';
import { useLocation } from 'react-router-dom';

function BrandList() {
  const location = useLocation();
  const brands = location.state?.brands || [];

  return (
    <div style={{ padding: '20px' }}>
      <h1>추천 리스트 생성</h1>
      <div>
        {brands.length === 0 ? (
          <p>No brands found.</p>
        ) : (
          <ul>
            {brands.map((brand, index) => (
              <li key={index}>{brand}</li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

export default BrandList;
