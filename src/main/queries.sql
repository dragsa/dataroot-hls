-- select avg(mark) from visits WHERE location = 1;
SELECT avg(mark) from visits JOIN users on "user" = users.id where location = 1 and gender = 'f';