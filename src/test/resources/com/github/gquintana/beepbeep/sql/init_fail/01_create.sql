CREATE TABLE person(
  login VARCHAR (64) PRIMARY KEY ,
  email VARCHAR (256) ERROR
);

CREATE INDEX person_email ON person(email);
