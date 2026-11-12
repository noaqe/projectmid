CREATE TABLE projects (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE CHECK (btrim(name) <> '')
);

CREATE TABLE assignees (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(100) NOT NULL CHECK (btrim(name) <> ''),
    email varchar(160) NOT NULL UNIQUE
);

CREATE TABLE statuses (
    code varchar(20) PRIMARY KEY,
    name varchar(60) NOT NULL UNIQUE
);

CREATE TABLE tasks (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title varchar(160) NOT NULL CHECK (btrim(title) <> ''),
    description varchar(1000) NOT NULL DEFAULT '',
    project_id bigint NOT NULL REFERENCES projects(id) ON DELETE RESTRICT,
    assignee_id bigint REFERENCES assignees(id) ON DELETE RESTRICT,
    status_code varchar(20) NOT NULL REFERENCES statuses(code) ON DELETE RESTRICT,
    due_date date
);

CREATE INDEX tasks_project_idx ON tasks(project_id);
