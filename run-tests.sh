#!/bin/bash

# BungeeJustice Test Runner Script
# Usage: ./run-tests.sh [all|quick|specific]

set -e

COLOR_GREEN='\033[0;32m'
COLOR_RED='\033[0;31m'
COLOR_BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${COLOR_BLUE}=== BungeeJustice Test Suite ===${NC}\n"

MODE="${1:-all}"

case $MODE in
    all)
        echo -e "${COLOR_BLUE}Running all tests...${NC}"
        mvn clean test
        ;;
    quick)
        echo -e "${COLOR_BLUE}Running quick tests (skipping slow tests)...${NC}"
        mvn test -Dgroups="!slow"
        ;;
    package)
        echo -e "${COLOR_BLUE}Running tests as part of package build...${NC}"
        mvn clean package
        ;;
    punish)
        echo -e "${COLOR_BLUE}Running PunishCommand tests...${NC}"
        mvn test -Dtest=PunishCommandTest
        ;;
    unpunish)
        echo -e "${COLOR_BLUE}Running UnpunishCommand tests...${NC}"
        mvn test -Dtest=UnpunishCommandTest
        ;;
    simple)
        echo -e "${COLOR_BLUE}Running SimpleCommands tests...${NC}"
        mvn test -Dtest=SimpleCommandsTest
        ;;
    manager)
        echo -e "${COLOR_BLUE}Running PunishmentManager tests...${NC}"
        mvn test -Dtest=PunishmentManagerTest
        ;;
    punishment)
        echo -e "${COLOR_BLUE}Running Punishment class tests...${NC}"
        mvn test -Dtest=PunishmentTest
        ;;
    *)
        echo -e "${COLOR_RED}Unknown option: $MODE${NC}"
        echo "Usage: $0 [all|quick|package|punish|unpunish|simple|manager|punishment]"
        echo ""
        echo "Options:"
        echo "  all       - Run all tests"
        echo "  quick     - Run all tests except slow ones"
        echo "  package   - Run tests as part of package build"
        echo "  punish    - Run PunishCommand tests only"
        echo "  unpunish  - Run UnpunishCommand tests only"
        echo "  simple    - Run SimpleCommands tests only"
        echo "  manager   - Run PunishmentManager tests only"
        echo "  punishment - Run Punishment class tests only"
        exit 1
        ;;
esac

if [ $? -eq 0 ]; then
    echo -e "\n${COLOR_GREEN}✓ Tests passed!${NC}\n"
else
    echo -e "\n${COLOR_RED}✗ Tests failed!${NC}\n"
    exit 1
fi
