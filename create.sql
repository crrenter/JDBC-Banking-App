-- P1
-- Section 5
-- Student ID: 006152874
-- Renteria, Carlos,

create table p1.Customer (
	id		integer		not null generated always as identity 
	(start with 100 increment by 1) primary key,
	name	varchar(15)	not null,
	gender	char		check (gender in ('F', 'M')) not null,
	age		integer		check (age >= 0)	not null,
	pin		integer		check (pin >= 0)	not null
);

create table p1.account (
	number		integer		not null generated always as identity 
	(start with 1000 increment by 1) primary key,
	id			integer 	not null references p1.Customer(id),
	balance 	integer 	not null	check (balance >= 0),
	type		char		not null	check (type in ('C', 'S')),
	status		char		not null 	check (status in ('A', 'I'))
);