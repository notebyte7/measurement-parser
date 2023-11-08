CREATE TABLE IF NOT EXISTS meas (
meas_id             BIGINT			                GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
datetime 		    TIMESTAMP WITHOUT TIME ZONE     not null ,
lat 				NUMERIC(8,6)	                NOT NULL,
lon 				NUMERIC(9,6) 	                NOT NULL,
system              INTEGER                         NOT NULL,
cell_type           INTEGER                         NOT NULL,
band                VARCHAR(20)                     NOT NULL,
channel             INTEGER                         NOT NULL,
identity            INTEGER                         NOT NULL,
power               NUMERIC(4,1)                    not null,
quality             NUMERIC(3,1)
);