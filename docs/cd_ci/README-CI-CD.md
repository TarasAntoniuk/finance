# CI/CD Setup Guide

## 📋 Table of Contents
1. [Quick Start](#quick-start)
2. [GitHub Configuration](#github-configuration)
3. [Workflow Structure](#workflow-structure)
4. [Deploy Process](#deploy-process)
5. [Troubleshooting](#troubleshooting)

---

## 🚀 Quick Start

### 1. Create required branches
```bash
# Create develop (if not exists)
git checkout -b develop
git push origin develop

# Create stable (production branch)
git checkout -b stable
git push origin stable

# Return to develop for work
git checkout develop
```

### 2. Create file structure
```
.github/
├── workflows/
│   └── ci-cd.yml
├── badges/
└── pull_request_template.md
dependency-check-suppressions.xml
Dockerfile
.dockerignore
```

### 3. Configure GitHub Secrets
Navigate: **Settings → Secrets and variables → Actions → New repository secret**

#### Required secrets:
- `DOCKERHUB_USERNAME` - your Docker Hub username
- `DOCKERHUB_TOKEN` - Docker Hub access token

#### Optional secrets:
- `SONAR_TOKEN` - for SonarCloud analysis
- `SLACK_WEBHOOK` - for Slack notifications
- `CODECOV_TOKEN` - for Codecov integration

---

## ⚙️ GitHub Configuration

### Branch Protection Rules

#### For `stable` branch:
1. **Settings → Branches → Add rule**
2. Branch name pattern: `stable`
3. Settings:
    - ✅ Require a pull request before merging
    - ✅ Require approvals: **2**
    - ✅ **Allow squash merging ONLY**
    - ✅ **Default to pull request title for squash merge commits**
    - ✅ Require status checks to pass:
        - `Build and Test`
        - `Security Vulnerability Scan`
        - `Code Quality Analysis`
    - ✅ Require conversation resolution
    - ✅ Do not allow bypassing

#### Additional merge settings for `stable`:
**Settings → General → Pull Requests**
- ✅ Allow squash merging
- ❌ Allow merge commits (disable)
- ❌ Allow rebase merging (disable)

**Result:** All commits from develop will be squashed into ONE commit when merging to stable.

#### For `develop` branch:
1. Branch name pattern: `develop`
2. Settings:
    - ✅ Require a pull request before merging
    - ✅ Require approvals: **1**
    - ✅ Require status checks to pass:
        - `Build and Test`
        - `Security Vulnerability Scan`
    - ✅ Require conversation resolution

---

## 🔄 Workflow Structure

### Job 1: Build and Test
- Runs for all pushes and PRs
- Starts PostgreSQL in Docker
- Executes `mvn clean verify`
- Generates JaCoCo coverage report
- Uploads results to artifacts

**Execution time:** ~3-5 minutes

### Job 2: Security Scan
- Runs after successful build
- Uses OWASP Dependency Check
- Checks known vulnerabilities (CVE)
- Fails build if CVSS >= 7

**Execution time:** ~2-3 minutes

### Job 3: Code Quality
- SonarCloud code analysis
- Checks code smells, bugs, vulnerabilities
- Requires code quality A or B

**Execution time:** ~2-4 minutes

### Job 4: Build Docker Image
- Runs only for `develop`
- Creates Docker image
- Pushes to Docker Hub with tags

**Execution time:** ~3-5 minutes

### Job 5: Deploy to Stable
- Runs only for PRs to `stable`
- Checks coverage >= 80%
- Automatic merge after approval

**Execution time:** ~1 minute

---

## 🎯 Deploy Process to Stable

### Workflow diagram:
```
feature/new-feature → develop → stable
       ↓                ↓          ↓
    Tests           Tests +     Deploy
                    Review +   (Production)
                    Docker
```

### Step 1: Feature Development
```bash
# Create feature branch
git checkout -b feature/add-new-endpoint
git push origin feature/add-new-endpoint

# After completion - create PR to develop
```

### Step 2: Merge to develop
```bash
# PR: feature/add-new-endpoint → develop
# GitHub Actions will automatically run:
# - Build and Test
# - Security Scan
# - Code Quality Analysis
# - Build Docker Image

# After approval (1 reviewer) - merge
```

### Step 3: Deploy to stable (Production)
```bash
# Create PR from develop to stable
git checkout develop
git pull origin develop
gh pr create --base stable --head develop --title "Release v1.2.3" --label "ready-for-stable"

# GitHub Actions will run ALL checks:
# ✅ Build and Test
# ✅ Security Vulnerability Scan (fail if CVSS >= 7)
# ✅ Code Quality Analysis
# ✅ Coverage check (minimum 80%)

# Requires 2 approvals from team members
# After approval - automatic SQUASH MERGE to stable
# All commits from develop → ONE commit in stable
```

**Important:** When merging to stable, use a descriptive PR title like:
- `Release v1.2.3 - Add payment processing`
- `Hotfix v1.2.4 - Fix authentication bug`

This title will become the single commit message in stable.

---

## 📊 Monitoring and Badges

### Add badges to README.md:

```markdown
[![CI/CD](https://github.com/yourusername/finance-core/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/yourusername/finance-core/actions/workflows/ci-cd.yml)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/yourusername/finance-core/actions)
[![codecov](https://codecov.io/gh/yourusername/finance-core/branch/main/graph/badge.svg)](https://codecov.io/gh/yourusername/finance-core)
```

### View results:
- **Actions tab** - all workflow runs
- **Pull requests** - status checks for each PR
- **Artifacts** - test reports, coverage reports

---

## 🔧 Local Testing Before Push

### Run all tests:
```bash
mvn clean verify -P test-coverage
```

### Check coverage:
```bash
mvn jacoco:report
open target/site/jacoco/index.html
```

### Check security vulnerabilities:
```bash
mvn org.owasp:dependency-check-maven:check
open target/dependency-check-report.html
```

### Run with Docker:
```bash
# Build image
docker build -t finance-core:local .

# Run container
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/finance \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  finance-core:local
```

---

## 🐛 Troubleshooting

### Tests fail locally
```bash
# Clear Maven cache
mvn clean
rm -rf ~/.m2/repository

# Reinstall
mvn clean install
```

### Docker build fails
```bash
# Check Java version
java --version  # Should be 21

# Check Dockerfile syntax
docker build --no-cache -t finance-core:test .
```

### GitHub Actions fails
1. Check **Actions** tab in GitHub
2. Click on failed job
3. Review logs
4. Fix the issue locally
5. Push again

### Coverage below threshold
```bash
# Generate coverage report
mvn jacoco:report

# Open report
open target/site/jacoco/index.html

# Add missing tests for uncovered code
```

### Security vulnerabilities found
```bash
# View detailed report
mvn org.owasp:dependency-check-maven:check
open target/dependency-check-report.html

# Update vulnerable dependencies in pom.xml
# Or add suppression in dependency-check-suppressions.xml
```

---

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [OWASP Dependency Check](https://jeremylong.github.io/DependencyCheck/)
- [SonarCloud Documentation](https://docs.sonarcloud.io/)

---

## ✅ Checklist

Before pushing to stable, ensure:
- [ ] All tests pass locally
- [ ] Code coverage >= 80%
- [ ] No security vulnerabilities (CVSS >= 7)
- [ ] Code reviewed by at least 2 team members
- [ ] Documentation updated
- [ ] CHANGELOG.md updated with changes
- [ ] Version number incremented