-- Example SQL query to fetch test data from a database
-- This would be executed through the SqlClient in actual tests

SELECT 
    id,
    name,
    username,
    email,
    phone
FROM 
    users
WHERE 
    status = 'active'
ORDER BY 
    id ASC
LIMIT 5;