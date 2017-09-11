-- Function: "getMissingTimes"(character varying, character varying, character varying)

-- DROP FUNCTION "getMissingTimes"(character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION "getMissingTimes"("dataInicio" character varying, "dataFim" character varying, intervalo character varying)
  RETURNS SETOF timestamp without time zone AS
$BODY$SELECT serie.dia
FROM (
select generate_series($1::timestamp, $2::timestamp, $3::interval) as dia
) serie 
left join t_dado_ponto_grade dado
on serie.dia = dado.dataprevisao
where  dado.ponto_grade_id is null
order by serie.dia$BODY$
  LANGUAGE sql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION "getMissingTimes"(character varying, character varying, character varying)
  OWNER TO postgres;