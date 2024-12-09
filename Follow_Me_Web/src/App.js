import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { CustomerProvider } from './CustomerContext';
import Select1 from './components/Select1';
import SelectInfo from './components/SelectInfo';
import SelectCategory from './components/SelectCategory';
import ClothSelect from './components/ClothSelect';
import BrandFilter from './components/BrandFilter';

const App = () => {
  return (
    <CustomerProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Select1 />} />
          <Route path="/select/select_info" element={<SelectInfo />} />
          <Route path="/select/select_category" element={<SelectCategory />} />
          <Route path="/select/cloth_select" element={<ClothSelect />} />
          <Route path="/select/BrandFilter" element={<BrandFilter />} />
        </Routes>
      </Router>
    </CustomerProvider>
  );
};

export default App;
