import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/ClothSelect.css';

const styleOptions = [
  { label: '힙한', value: '힙한' },
  { label: '고급스러운', value: '고급스러운' },
  { label: '깔끔한', value: '깔끔한' },
  { label: '편한', value: '편한' },
  { label: '격식있는', value: '격식있는' },
  { label: '러블리한', value: '러블리한' },
  { label: '시크한', value: '시크한' },
  { label: '트렌디한', value: '트렌디한' },
  { label: '스포티한', value: '스포티한' },
  { label: '비지니스캐주얼', value: '비지니스캐주얼' },
  { label: '일상적인', value: '일상적인' },
  { label: '골프웨어', value: '골프웨어' },
  { label: '아웃도어', value: '아웃도어' },
];

const ClothSelect = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [selectedStyles, setSelectedStyles] = useState([]);
  const [loading, setLoading] = useState(false);
  const { id, gender, ages } = location.state || {};
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
  // 체크박스 선택 핸들러
  const handleTagClick = (style) => {
    if (selectedStyles.includes(style)) {
      setSelectedStyles(selectedStyles.filter((s) => s !== style)); // 선택 해제
    } else if (selectedStyles.length < 2) {
      setSelectedStyles([...selectedStyles, style]); // 추가
    } else {
      alert('최대 2개까지 선택 가능합니다.'); // 초과 시 경고 메시지
    }
  };

  // 폼 제출 핸들러
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (selectedStyles.length === 0) {
      alert('스타일을 하나 이상 선택해주세요.');
      return;
    }
    setLoading(true);
    try {
      await axios.patch(`http://localhost:5000/user_selections/${id}`, {
        category: '의류',
        style: selectedStyles,
      });
  
      const response = await axios.post('http://localhost:5000/filter_brands', {
        gender,
        ages,
        category: '의류',
        style: selectedStyles,
      });
  
      navigate('/select/filter_brands', { state: response.data });
    } catch (error) {
      console.error('Error submitting styles:', error);
    } finally {
      setLoading(false);
    }
  };
  const handleGoHome = () => {
    navigate('/');
  };
  return (
    <div style={containerStyle} className="container">
      <h2>스타일을 선택하세요.</h2>
      <div className="separator"></div>
      <div className="background-image">
        <form onSubmit={handleSubmit}>
          <div className="style-container">
            {styleOptions.map(({ label, value }) => (
              <button
                key={value}
                type="button"
                className={`style-tag ${selectedStyles.includes(value) ? 'selected' : ''}`}
                onClick={() => handleTagClick(value)}
              >
                #{label}
              </button>
            ))}
          </div>
          <button type="submit" className="submit-button" disabled={loading}>
            {loading ? '저장 중...' : '다음으로'}
          </button>
        </form>
      </div>
      <button className="home-button" onClick={handleGoHome}>
        Home
      </button>
    </div>
  );
};

export default ClothSelect;