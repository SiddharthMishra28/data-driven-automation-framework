#!/bin/bash

# Define colors for better readability
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Starting Test Execution with Development Profile (No Database Connections)...${NC}"

# Run the tests with Maven using dev profile
echo -e "${BLUE}Running tests with Maven...${NC}"
mvn clean test -Denv=dev

# Check if tests were executed successfully
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Tests executed successfully!${NC}"
else
    echo -e "${YELLOW}Some tests failed. Check logs for details.${NC}"
    echo -e "${BLUE}Continuing with report generation...${NC}"
fi

# Generate Allure report
echo -e "${BLUE}Generating Allure report...${NC}"
mvn allure:report

# Check if report was generated successfully
if [ -d "target/site/allure-maven-plugin" ]; then
    echo -e "${GREEN}Allure Report generated successfully!${NC}"
    echo -e "${BLUE}You can find the report at:${NC} target/site/allure-maven-plugin/index.html"
else
    echo -e "${YELLOW}Failed to generate Allure report.${NC}"
fi

echo -e "${BLUE}Process completed!${NC}"