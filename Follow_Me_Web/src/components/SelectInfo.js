import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/SelectInfo.css';

const SelectInfo = () => {
  const [gender, setGender] = useState('');
  const [ages, setAges] = useState('');

  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
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
      const { id } = response.data;
      navigate('/select/select_category', { state: { id: response.data.id, gender, ages } }); // 다음 페이지로 이동하면서 id 전달
    } catch (error) {
      console.error('Error submitting data:', error);
      setLoading(false);
    }
  };

  return (
    <div className="select-info-container">
      <header className="select-info-header">
        <h1>Follow Me</h1>
        <h2>쇼핑 정보 입력</h2>
      </header>
      <main className="select-info-form">
        <form>
          <div className="form-gender">
            <label>성별</label>
            <div className="radio-group">
              <label>
                <input type="radio" name="gender" value="여성"
                onChange={(e) => setGender(e.target.value)}/>
                여성
              </label>
              <label>
                <input type="radio" name="gender" value="남성"
                onChange={(e) => setGender(e.target.value)}/>
                남성
              </label>
            </div>
          </div>

          <div className="form-ages">
            <label>나이</label>
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
            {loading ? '저장 중...' : '제출'}
          </button>
        </form>
      </main>
    </div>
  );
};

export default SelectInfo;
