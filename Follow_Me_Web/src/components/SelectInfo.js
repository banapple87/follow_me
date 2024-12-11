import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/SelectInfo.css';

const SelectInfo = () => {
  const [gender, setGender] = useState('');
  const [ages, setAges] = useState('');

  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const containerStyle = {
    backgroundImage: `url(/background_2.png)`,
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

  const handleGenderClick = (selectedGender) => {
    setGender(selectedGender);
  };
  const handleSubmit = async (e) => {
    e.preventDefault(); // 기본 폼 제출 방지
    setLoading(true);

    try {
      const response = await axios.post('http://localhost:5000/user_selections', {
        gender: gender,
        ages: ages,
        category: '',
        style: [],
      });
      
      navigate('/select/select_category', { state: { id: response.data.id, gender, ages } }); // 다음 페이지로 이동하면서 id 전달
    } catch (error) {
      console.error('Error submitting data:', error);
      setLoading(false);
    }
  };

  const handleGoHome = () => {
    navigate('/');
  };

  return (
    <div style={containerStyle} className="select-info-container">
      <header className="select-info-header">
        <h2>쇼핑 정보 입력</h2>
      </header>
      <div className="separator"></div>
      <main className="select-info-form">
        <form>
          <div className="form-gender">
            <div className="button-group">
              <button
                type="button"
                className={`gender-button ${gender === '여성' ? 'active' : ''}`}
                onClick={() => handleGenderClick('여성')}
              >
                여성
              </button>
              <button
                type="button"
                className={`gender-button ${gender === '남성' ? 'active' : ''}`}
                onClick={() => handleGenderClick('남성')}
              >
                남성
              </button>
            </div>
          </div>

          <div className="form-ages">
            <div className="dropdown-group">
              <select onChange={(e) => setAges(e.target.value)}>
                <option value="">연령대 선택</option>
                <option value="3-6세">3-6세</option>
                <option value="7-10세">7-10세</option>
                <option value="10대">10대</option>
                <option value="20대">20대</option>
                <option value="30대">30대</option>
                <option value="40대">40대</option>
                <option value="50대">50대</option>
                <option value="60대 이상">60대 이상</option>
              </select>
            </div>
          </div>

          <button type="submit" className="submit-button" onClick={handleSubmit} disabled={loading}>
            {loading ? '저장 중...' : '정보입력'}
          </button>
        </form>
      </main>

      <button className="home-button" onClick={handleGoHome}>
        Home
      </button>
    </div>
  );
};

export default SelectInfo;
