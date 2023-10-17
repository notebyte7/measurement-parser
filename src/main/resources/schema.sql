CREATE TABLE IF NOT EXISTS meas (
    meas_id             BIGINT			                GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    datetime 		    TIMESTAMP WITHOUT TIME ZONE     not null ,
    lat 				NUMERIC(8,6)	                NOT NULL,
    lon 				NUMERIC(9,6) 	                NOT NULL,
    system              VARCHAR(10)                     NOT NULL,
    cell_type            BIGINT                          NOT NULL,
    band                VARCHAR(20)                     NOT NULL,
    channel             BIGINT                          NOT NULL,
    identity            BIGINT                          NOT NULL,
    power               NUMERIC(4,1)                    not null,
    quality             NUMERIC(3,1)
);