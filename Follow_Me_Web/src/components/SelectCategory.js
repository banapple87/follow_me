import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import '../styles/SelectCategory.css';

const SelectCategory = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const { id, gender, ages } = location.state || {};

  const navigateToNextPage = (selectedCategory) => {
    const pageMapping = {
      의류: '/select/cloth_select',
      수영복: '/select/swimwear',
      이너웨어: '/select/innerwear',
      패션잡화: '/select/acc_select',
      화장품: '/select/cosmetics',
      명품: '/select/luxury',
    };
    const nextPage = pageMapping[selectedCategory];
    if (nextPage) {
      navigate(nextPage, { state: { id, gender, ages, category: selectedCategory } });
    } else {
      console.error('Invalid category selected');
    }
  };

  const handleCategoryClick = async (selectedCategory) => {
    setLoading(true);

    try {
      console.log('Sending PATCH request for ID:', id, 'Category:', selectedCategory);
      const response = await axios.patch(`http://localhost:5000/user_selections/${id}`, { category:selectedCategory });
      if (response.status === 200) {
        console.log('Category updated successfully:', response.data);
        navigateToNextPage(selectedCategory);
      } else {
        console.error('Failed to update category:', response.data);
      }
    } catch (error) {
      console.error('Error updating category:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <h1>Follow Me</h1>
      <h2>카테고리를 선택해주세요</h2>
      <div className="separator"></div>
      <div className="button-group">
        <button
          type="button" className="category-button"
          onClick={() => handleCategoryClick('의류')}
          disabled={loading}
        >
          의류
        </button>
        <button
          type="button" className="category-button"
          onClick={() => handleCategoryClick('수영복')}
          disabled={loading}
        >
          수영복
        </button>
        <button
          type="button" className="category-button"
          onClick={() => handleCategoryClick('이너웨어')}
          disabled={loading}
        >
          이너웨어
        </button>
        <button
          type="button" className="category-button"
          onClick={() => handleCategoryClick('패션잡화')}
          disabled={loading}
        >
          패션잡화
        </button>
        <button
          type="button" className="category-button"
          onClick={() => handleCategoryClick('화장품')}
          disabled={loading}
        >
          코스메틱
        </button>
        <button
          type="button" className="category-button"
          onClick={() => handleCategoryClick('명품')}
          disabled={loading}
        >
          명품관
        </button>
      </div>
    </div>
  );
};

export default SelectCategory;
