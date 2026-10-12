import { useNavigate } from 'react-router-dom';

function Home() {
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login', { replace: true });
      };
    return (
        <div>
            <h1>cLockedIn</h1>
            <p>You are logged in</p>
            <button onClick={handleLogout}>Logout</button>
        </div>
    );
}

export default Home;