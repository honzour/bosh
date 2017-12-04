alter table shops add nid int;

-- provest import

update photos p set p.nid = (select s.nid from shops s where s.id = p.id);

alter table photos add column nid int;
update photos set shop = nid;
alter table photos drop column nid;
alter table shops drop column id;
alter table shops add column id int;
update shops set id = nid;

alter table shops drop column nid;



