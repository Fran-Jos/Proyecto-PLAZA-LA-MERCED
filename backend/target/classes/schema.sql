ALTER TABLE cash_sessions ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE sales ADD COLUMN IF NOT EXISTS cash_session_id BIGINT;
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_sales_cash_session'
    ) THEN
        ALTER TABLE sales
            ADD CONSTRAINT fk_sales_cash_session
            FOREIGN KEY (cash_session_id) REFERENCES cash_sessions(id);
    END IF;
END $$;
