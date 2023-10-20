.PHONY = venv fix run

APP_DIR=smsc_client

export PYTHONPATH=.

venv:
	rm -rf ".venv"
	poetry install
	@printf "\nDone. You can now activate the virtual environment:\n  source .venv/bin/activate\n"

fix:
	poetry run black $(APP_DIR)
	poetry run flake8 $(APP_DIR)
	poetry run isort $(APP_DIR)
	poetry run ruff $(APP_DIR)/**

run:
	poetry run python smsc_client/client.py
