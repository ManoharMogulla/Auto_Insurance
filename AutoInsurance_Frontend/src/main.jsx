import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import './index.css'
import QuoteFlow from './components/QuoteFlow.jsx'
import CustomerLogin from './components/CustomerLogin.jsx'
import CustomerRegistration from './components/CustomerRegistration.jsx'
import CustomerSummary from './components/CustomerSummary.jsx'
import ClaimsPage from './components/ClaimsPage.jsx'
import { AuthProvider } from './auth/AuthContext.jsx'
import { useAuth } from './auth/authContext.js'
import ProtectedRoute from './auth/ProtectedRoute.jsx'

function App() {
  const { authenticated } = useAuth()

  return (
    <Routes>
      <Route path="/login" element={<CustomerLogin />} />
      <Route path="/register" element={<CustomerRegistration />} />
      <Route path="/customer-summary" element={<ProtectedRoute><CustomerSummary /></ProtectedRoute>} />
      <Route path="/claims" element={<ProtectedRoute><ClaimsPage /></ProtectedRoute>} />
      <Route path="/quote/*" element={<ProtectedRoute><QuoteFlow /></ProtectedRoute>} />
      <Route path="/policy-details" element={<ProtectedRoute><QuoteFlow /></ProtectedRoute>} />
      <Route path="/" element={<Navigate to={authenticated ? '/customer-summary' : '/login'} replace />} />
      <Route path="*" element={<Navigate to={authenticated ? '/customer-summary' : '/login'} replace />} />
    </Routes>
  )
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)

export default App
