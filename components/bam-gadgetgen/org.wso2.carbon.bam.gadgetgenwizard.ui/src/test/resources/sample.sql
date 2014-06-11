create database gadgetgen;
use gadgetgen;
create table productSummary (prod_id integer NOT NULL, prod_name varchar (50) NOT NULL, quantity_sold integer NOT NULL, total_amount double NOT NULL, PRIMARY KEY (prod_id)) ;
insert into productSummary values (0124, 'Milk', 563, 24500.00);
insert into productSummary values (0235, 'Bread', 5604, 124900.00);
insert into productSummary values (0346, 'Butter', 320, 16900.00);
insert into productSummary values (0055, 'Sugar', 3760, 34800.00);
