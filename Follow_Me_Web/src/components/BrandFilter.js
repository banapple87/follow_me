import React, { useState, useEffect } from 'react';
import axios from 'axios';

const BrandFilter = ({ gender, ages, category, style }) => {
  const [brands, setBrands] = useState([]); // 필터링된 브랜드 목록
  const [loading, setLoading] = useState(false); // 로딩 상태
  const [error, setError] = useState(null); // 에러 상태
  const [filterLog, setFilterLog] = useState(''); // Filter result 로그

  // 서버로부터 데이터를 가져오는 함수
  const fetchBrands = async () => {
    setLoading(true);
    setError(null);
    try {
      // 서버에 POST 요청
      const response = await axios.post('http://localhost:5000/filter_brands', {
        gender, 
        ages, 
        category, 
        style,
      });
      console.log('API 응답 데이터:', response.data);

      // 응답 데이터가 배열이라면 각 요소에서 브랜드만 추출
      if (response.data && Array.isArray(response.data)) {
        const brandList = response.data.map((item) => item.brand); // brand 속성만 추출
        setBrands(brandList); // 브랜드 목록 상태 업데이트
      } else {
        setBrands([]); // 빈 배열로 설정 (응답이 없으면)
      }

      setFilterLog(`Filter result: ${JSON.stringify(response.data)}`);
    } catch (error) {
      console.error('Error fetching brands:', error.response?.data || error.message);
      setError(error.response?.data?.error || '브랜드를 가져오는 중 문제가 발생했습니다.');
    } finally {
      setLoading(false); // 요청 완료 후 로딩 상태 종료
    }
  };

  // 필터 조건이 변경될 때마다 서버로 요청
  useEffect(() => {
    if (gender && ages && category && style) {
      fetchBrands();
    }
  }, [gender, ages, category, style]);

  return (
    <div>
      <h1>추천 브랜드</h1>
      {loading ? (
        <p>로딩 중...</p> // 로딩 중일 때 표시
      ) : error ? (
        <p style={{ color: 'red' }}>{error}</p> // 에러 메시지
      ) : (
        <>
          <ul>
            {brands.length > 0 ? (
              brands.map((brand, index) => (
                <li key={index}>{brand}</li> // 결과 리스트 출력
              ))
            ) : (
              <p>조건에 맞는 브랜드가 없습니다.</p> // 결과가 없는 경우
            )}
          </ul>

          {/* Filter result 로그 출력 */}
          <div style={{ marginTop: '20px', color: 'gray' }}>
            <h3>Filter result 로그</h3>
            <pre>{filterLog}</pre>
          </div>
        </>
      )}
    </div>
  );
};

export default BrandFilter;
