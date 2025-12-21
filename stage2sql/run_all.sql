-- Run all scripts in order

\echo '1. Resetting database...'
\i reset_db.sql

\echo '2. Creating tables...'
\i create_tables.sql

\echo '3. Creating triggers...'
\i create_triggers.sql

\echo '4. Creating functions...'
\i create_functions.sql

\echo '5. Creating indexes...'
\i create_indexes.sql

\echo '6. Inserting test data...'
\i test_data.sql

\echo '7. Testing functions and triggers...'
\i test_functions.sql

\echo 'Done! Database created and tested successfully.';
