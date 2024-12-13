import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import '../styles/SelectCategory.css';

const SelectCategory = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
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
    padding: '20px',
  };

  const handleCategoryClick = async (selectedCategory) => {
    setLoading(true);

    try {
  
      await axios.patch(`http://localhost:5000/user_selections/${id}`, {
        category: selectedCategory,
        style: null,
      });

      if (selectedCategory === '의류') {
        navigate('/select/cloth_select', { state: { id, gender, ages, category: selectedCategory } });
      } else if (selectedCategory === '명품' || selectedCategory === '화장품') {
        const response = await axios.post('http://localhost:5000/filter_brands', {
          category: selectedCategory,
          style: null,
        });
        navigate('/select/filter_brands', { state: response.data });
      } else {
        const response = await axios.post('http://localhost:5000/filter_brands', {
          gender,
          ages,
          category: selectedCategory,
          style: null,
        });
        navigate('/select/filter_brands', { state: response.data });
      }
    } catch (error) {
      console.error('Error updating category:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleGoHome = () => {
    navigate('/');
  };

  return (
    <div style={containerStyle} className="container">
      <h2>카테고리를 선택하세요.</h2>
      <div className="separator"></div>
      <div className="button-group">
        {['의류', '수영복', '이너웨어', '패션잡화', '화장품', '명품'].map((category) => (
          <button
            key={category}
            type="button"
            className="category-button"
            onClick={() => handleCategoryClick(category)}
            disabled={loading}
          >
            {category}
          </button>
        ))}
      </div>
      <button className="home-button" onClick={handleGoHome} disabled={loading}>
        Home
      </button>
    </div>
  );
};

export default SelectCategory;
