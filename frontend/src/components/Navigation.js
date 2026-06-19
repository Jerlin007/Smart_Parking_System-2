import React from 'react';
import { Navbar, Nav, Container, Button, Badge } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navigation = () => {
  const { isAuthenticated, user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <Navbar expand="lg" className="shadow-sm">
      <Container>
        <Navbar.Brand as={Link} to="/" className="fw-bold">
          <span style={{ color: '#667eea' }}>Smart</span>{' '}
          <span style={{ color: '#764ba2' }}>Parking</span>
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          {isAuthenticated ? (
            <Nav className="me-auto">
              <Nav.Link as={Link} to="/" className="mx-1">Dashboard</Nav.Link>
              <Nav.Link as={Link} to="/vehicles" className="mx-1">Vehicles</Nav.Link>
              <Nav.Link as={Link} to="/reservations" className="mx-1">Reservations</Nav.Link>
              <Nav.Link as={Link} to="/parking" className="mx-1">Entry/Exit</Nav.Link>
              <Nav.Link as={Link} to="/availability" className="mx-1">Search</Nav.Link>
              {isAdmin && (
                <>
                  <Nav.Link as={Link} to="/lots" className="mx-1">Lots</Nav.Link>
                  <Nav.Link as={Link} to="/slots" className="mx-1">Slots</Nav.Link>
                  <Nav.Link as={Link} to="/billing" className="mx-1">Billing</Nav.Link>
                  <Nav.Link as={Link} to="/admin/users" className="mx-1">Users</Nav.Link>
                </>
              )}
            </Nav>
          ) : (
            <Nav className="me-auto">
              <Nav.Link as={Link} to="/login">Login</Nav.Link>
              <Nav.Link as={Link} to="/register">Register</Nav.Link>
            </Nav>
          )}
          {isAuthenticated && (
            <Nav className="align-items-center">
              <div className="d-flex align-items-center me-3">
                <div
                  className="rounded-circle d-flex align-items-center justify-content-center me-2"
                  style={{
                    width: '36px',
                    height: '36px',
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    color: 'white',
                    fontWeight: '600',
                    fontSize: '0.9rem'
                  }}
                >
                  {user?.username?.charAt(0).toUpperCase()}
                </div>
                <div className="d-flex flex-column">
                  <small className="fw-semibold">{user?.username}</small>
                  <Badge
                    bg={isAdmin ? 'primary' : 'secondary'}
                    style={{ fontSize: '0.65rem', width: 'fit-content' }}
                  >
                    {user?.role?.replace('ROLE_', '')}
                  </Badge>
                </div>
              </div>
              <Button
                variant="outline-danger"
                size="sm"
                onClick={handleLogout}
                style={{ borderRadius: '8px' }}
              >
                Logout
              </Button>
            </Nav>
          )}
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Navigation;