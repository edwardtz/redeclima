-- Script de criacao de BD

CREATE TABLE t_perfil
(
  id_perfil bigserial NOT NULL,
  nome character varying(255),
  CONSTRAINT t_perfil_pkey PRIMARY KEY (id_perfil )
);

create table t_usuario
(
  id_usuario bigserial NOT NULL,
  email character varying(255),
  nome character varying(255),
  id_perfil bigint NOT NULL,
  CONSTRAINT t_usuario_pkey PRIMARY KEY (id_usuario ),
  CONSTRAINT fkb3a6e663f4804b6a FOREIGN KEY (id_perfil)
      REFERENCES t_perfil (id_perfil) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE t_bacia
(
  id_bacia bigserial NOT NULL,
  nome character varying(255),
  nome_arquivo_dss character varying(255),
  nomeimagem character varying(255),
  x1 real NOT NULL,
  x2 real NOT NULL,
  y1 real NOT NULL,
  y2 real NOT NULL,
  CONSTRAINT t_bacia_pkey PRIMARY KEY (id_bacia )
);

CREATE TABLE t_estacao
(
  id_estacao bigserial NOT NULL,
  codigo integer,
  nome character varying(100),
  municipio character varying(50),
  bacia_id_bacia bigint,
  latitude real,
  longitude real,
  resposavel character varying(50),
  operador character varying(50),
  xutm bigint,
  yutm bigint,
  coordenadas character varying(100),
  altitude real,
  areadrenagem real,
  uf character varying(2),
  instalada boolean,
  tipotransmissao character varying(100),
  operadortelefonia character varying(50),
  telefone character varying(100),
  senha character varying(50),
  patrimonio character varying(100),
  abrigo character varying(100),
  painelsolar character varying(100),
  pluviometro character varying(100),
  pressao character varying(100),
  radar character varying(100),
  cabometros real,
  enderecoservidordados character varying(100),
  loginservidordados character varying(50),
  senhaservidordados character varying(50),
  dateiniciooperacao timestamp without time zone,
  tipodado character varying(50),
  observacoes character varying(255),
  estadoEstacao character varying (3),
  dataAtualizacaoEstado timestamp without time zone,
  CONSTRAINT t_estacao_pkey PRIMARY KEY (id_estacao ),
  CONSTRAINT fk6534e18b7d9a6ee9 FOREIGN KEY (bacia_id_bacia)
      REFERENCES t_bacia (id_bacia) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT t_estacao_codigo_key UNIQUE (codigo )
);

CREATE TABLE t_tipo_estacao
(
  id bigserial NOT NULL,
  codigo character varying(50) NOT NULL,
  descripcao character varying(100),
  CONSTRAINT t_tipo_estacao_pkey PRIMARY KEY (id ),
  CONSTRAINT t_tipo_estacao_codigo_key UNIQUE (codigo )
);

CREATE TABLE t_estacao_tipo_map
(
  estacoes_id_estacao bigint NOT NULL,
  tipos_id bigint NOT NULL,
  CONSTRAINT t_estacao_tipo_map_pkey PRIMARY KEY (tipos_id , estacoes_id_estacao ),
  CONSTRAINT fkf4dc4a05351be164 FOREIGN KEY (estacoes_id_estacao)
      REFERENCES t_estacao (id_estacao) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkf4dc4a0556f642af FOREIGN KEY (tipos_id)
      REFERENCES t_tipo_estacao (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE t_dados_estacao_telemetrica_15_min
(
  data timestamp without time zone NOT NULL,
  chuva double precision,
  nivel double precision,
  vazao double precision,
  estacao_id_estacao bigint NOT NULL,
  CONSTRAINT t_dados_estacao_telemetrica_15_min_pkey PRIMARY KEY (data , estacao_id_estacao ),
  CONSTRAINT fk8a53b7379b2394a3 FOREIGN KEY (estacao_id_estacao)
      REFERENCES t_estacao (id_estacao) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);




