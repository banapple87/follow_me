const express = require('express');
const cors = require('cors');
const app = express();
const PORT = 5000;
const mysql = require('mysql');

// 미들웨어 설정
app.use(express.json());
app.use(cors());

const userSelections = {};

const dbConfig = {
  host: '10.104.24.229',
  user: 'reactone',
  password: 'reactone123',
  database: 'user_db',
};

app.post('/user_selections', (req, res) => {
  const { gender, ages } = req.body;
  const id = Date.now();

  userSelections[id] = {
    gender,
    ages,
    category:null,
    style:null,
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
  const { id } = req.body;

  const user = userSelections[id];
  if (!user) {
    return res.status(404).json({ message: 'User not found' });
  }
  console.log('User data for filtering:', user);
  const { category, style, gender, ages } = user;

  const connection = mysql.createConnection(dbConfig);
  
  const whereConditions = [];
  const params = [];

  if (gender) {
    whereConditions.push('? IN (gender1, gender2)');
    params.push(gender);
  }
  if (ages) {
    whereConditions.push('? IN (age1, age2, age3, age4, age5, age6, age7, age8)');
    params.push(ages);
  }
  if (category) {
    whereConditions.push('category = ?');
    params.push(category);
  }
  if (style && style.length > 0) {
    const styleConditions = style
      .map(() => '(style1 = ? OR style2 = ? OR style3 = ?)')
      .join(' OR ');
    whereConditions.push(`(${styleConditions})`);
    style.forEach((s) => params.push(s, s, s));
  }

  const whereClause = whereConditions.length ? `WHERE ${whereConditions.join(' AND ')}` : '';
  const query = `SELECT DISTINCT brand FROM brand_info ${whereClause}`;

  // SQL 쿼리 출력
  console.log(`Executing SQL: ${query}`);
  console.log(`With parameters: ${params}`);

  connection.query(query, params, (err, results) => {
    connection.end();
    if (err) {
      console.error('Database error:', err.message);
      return res.status(500).json({ error: err.message });
    }

    const brands = results.map((row) => row.brand);
    res.status(200).json({ brands });
  });
});


// 서버 실행
app.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
});
