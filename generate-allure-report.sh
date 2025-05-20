#!/bin/bash

# Define colors for better readability
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Starting Allure Report Generation...${NC}"

# Make sure we have the right directory structure
mkdir -p target/allure-results

# Check if Allure command line is installed
if ! command -v allure &> /dev/null; then
    echo -e "${YELLOW}Allure command line tool not found. Using Maven plugin instead.${NC}"
    echo -e "${BLUE}Running: mvn allure:report${NC}"
    mvn allure:report
    
    # Check if report was generated
    if [ -d "target/site/allure-maven-plugin" ]; then
        echo -e "${GREEN}Report generated successfully!${NC}"
        echo -e "${BLUE}You can view the report by opening:${NC} target/site/allure-maven-plugin/index.html"
    else
        echo -e "${YELLOW}Failed to generate report using Maven plugin.${NC}"
        echo -e "Make sure you have run tests first with: mvn clean test"
    fi
else
    # Use Allure command line
    echo -e "${BLUE}Using Allure command line to generate report...${NC}"
    allure generate target/allure-results --clean -o target/allure-report
    
    # Check if report was generated
    if [ -d "target/allure-report" ]; then
        echo -e "${GREEN}Report generated successfully!${NC}"
        echo -e "${BLUE}Starting Allure server to serve the report...${NC}"
        allure serve target/allure-results
    else
        echo -e "${YELLOW}Failed to generate report.${NC}"
        echo -e "Make sure you have run tests first with: mvn clean test"
    fi
fi