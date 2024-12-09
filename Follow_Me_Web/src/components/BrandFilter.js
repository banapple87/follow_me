import React, { useState } from 'react';
import axios from 'axios';

const BrandFilter = ({ userId }) => {
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchBrands = async () => {
    setLoading(true);
    try {
      const response = await axios.post('http://localhost:5000/filter_brands', { id: userId });
      console.log('API 응답 데이터:', response.data);
      setBrands(response.data.sort()); // 브랜드 리스트 정렬
      setError(null);
    } catch (error) {
      console.error('Error fetching brands:', error.response?.data || error.message);
      setError(error.response?.data?.error || '브랜드를 가져오는 중 문제가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>추천 브랜드</h1>
      <button onClick={fetchBrands} disabled={loading}>
        {loading ? '로딩 중...' : '브랜드 리스트 찾는 중'}
      </button>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <ul>
        {brands.length > 0 ? (
          brands.map((brand, index) => <li key={index}>{brand}</li>)
        ) : (
          !loading && <p>조건에 맞는 브랜드가 없습니다.</p>
        )}
      </ul>
    </div>
  );
};

export default BrandFilter;
