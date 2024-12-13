const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const mysql = require('mysql');
const app = express();
const PORT = 5000;

// 미들웨어 설정
app.use(express.json());
app.use(cors());

// 비밀번호 파일에서 비밀번호 읽기
function getDbPassword() {
  try {
    const credentialsPath = path.join(__dirname, 'db_password.txt');
    return fs.readFileSync(credentialsPath, 'utf8').trim();
  } catch (error) {
    throw new Error('Database credentials file not found.');
  }
}

// MySQL 데이터베이스 연결 정보
const dbConfig = {
  host: '10.104.24.229',
  user: 'reactone',
  password: getDbPassword(),
  database: 'user_db',
};

const userSelections = {};

app.post('/user_selections', (req, res) => {
  const { gender, ages } = req.body;
  const id = Date.now();

  userSelections[id] = {
    gender,
    ages,
    category: null,
    style: null,
  };

  console.log(`User created: ID ${id}`, userSelections[id]);
  res.status(201).json({ message: 'User created', id });
});

app.patch('/user_selections/:id', (req, res) => {
  const id = req.params.id;
  if (!userSelections[id]) {
    return res.status(404).json({ message: 'User not found' });
  }
  const { category, style } = req.body;
  if (category !== undefined) {
    userSelections[id].category = category;
  }

  if (style !== undefined) {
    userSelections[id].style = style;
  }
  console.log(`User updated: ID ${id}`, userSelections[id]);
  res.status(200).json({ message: 'User updated', user: userSelections[id] });
});

app.get('/user_selections', (req, res) => {
  console.log('Fetching all users');
  res.status(200).json(userSelections);
});

app.post('/filter_brands', (req, res) => {
  const { gender, ages, category, style } = req.body;
  const whereConditions = [];
  const params = [];

  // gender 필터링
  if (gender) {
    whereConditions.push('(gender1 = ? OR gender2 = ?)');
    params.push(gender, gender);
  }

  // ages 필터링
  if (ages) {
    whereConditions.push('? IN (age1, age2, age3, age4, age5, age6, age7, age8)');
    params.push(ages);
  }

  // category 필터링
  if (category) {
    whereConditions.push('category = ?');
    params.push(category);
  }

  // style 필터링: category가 '의류'일 경우만 적용, style이 null이 아니면
  if (category === '의류' && style && style.length > 0) {
    const styleConditions = style
      .map(() => '(style1 = ? OR style2 = ? OR style3 = ?)')
      .join(' AND ');
    whereConditions.push(`(${styleConditions})`);
    style.forEach((s) => params.push(s, s, s));
  }

  // where 절 생성
  let whereClause = '';
  if (whereConditions.length > 0) {
    whereClause = `WHERE ${whereConditions.join(' AND ')}`;
  }

  // 최종 쿼리
  const query = `SELECT DISTINCT brand FROM brand_info ${whereClause}`;

  console.log(`Executing SQL: ${query}`);
  console.log(`With parameters: ${params}`);

  // MySQL 연결 및 쿼리 실행
  const connection = mysql.createConnection(dbConfig);
  connection.query(query, params, (err, results) => {
    connection.end();
    if (err) {
      console.error('Database error:', err.message);
      return res.status(500).json({ error: err.message });
    }

    console.log('Filter result:', results);
    const brands = results.map((row) => row.brand);
    res.status(200).json({ brands });
  });
});

// 리뷰 데이터 API
app.get('/api/reviews', (req, res) => {
  const connection = mysql.createConnection(dbConfig);
  connection.query('SELECT place_name, summary FROM summarized_reviews', (error, results) => {
    if (error) {
      console.error("DB 에러:", error);
      res.status(500).json({ error: "데이터를 가져오는 중 오류가 발생했습니다." });
    } else {
      res.json(results);
    }
    connection.end();
  });
});

// 서버 실행
app.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
});