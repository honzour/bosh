alter table shops add nid int;

-- provest import

alter table photos add nid int;
update photos p set p.nid = (select s.nid from shops s where s.id = p.id);
update photos set shop = nid;
alter table photos drop column nid;
alter table shops drop column id;
alter table shops add column id int;
update shops set id = nid;

alter table shops drop column nid;


alter table shops add column channel varchar(2);

update shops set channel = 'TP' where boss = 9;
update shops set channel = 'MP' where boss = 1;
