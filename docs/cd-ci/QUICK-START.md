# 🚀 Quick Start - CI/CD Setup

## Branch Structure
```
feature/* → develop → stable
```

- **feature/*** - development of new features
- **develop** - integration and testing (full commit history)
- **stable** - production (SQUASHED commits only!)

### Commit History Example:
```
develop:
├── feat: add user authentication
├── fix: correct validation logic
├── test: add unit tests
└── docs: update API documentation
          ↓ SQUASH MERGE
stable:
└── Release v1.2.0 - Add user authentication (ONE commit)
```

---

## 📦 Step 1: Create Branches

```bash
# If develop doesn't exist
git checkout -b develop
git push origin develop

# Create stable for production
git checkout -b stable
git push origin stable

# Return to develop
git checkout develop
```

---

## 📁 Step 2: Add Files to Repository

Create these files in your project:

```
project-root/
├── .github/
│   ├── workflows/
│   │   └── ci-cd.yml
│   └── pull_request_template.md
├── dependency-check-suppressions.xml
├── Dockerfile
└── .dockerignore
```

Copy content from the artifacts I created.

---

## 🔐 Step 3: Configure GitHub Secrets

**Settings → Secrets and variables → Actions**

### Required:
```bash
DOCKERHUB_USERNAME=your-username
DOCKERHUB_TOKEN=your-token
```

### Optional:
```bash
SONAR_TOKEN=your-sonar-token  # for code quality
SLACK_WEBHOOK=your-webhook    # for notifications
```

---

## 🛡️ Step 4: Branch Protection

### For `stable`:
**Settings → Branches → Add rule**

- Branch: `stable`
- ✅ Require pull request (2 approvals)
- ✅ **Allow squash merging ONLY**
- ✅ Require status checks:
    - Build and Test
    - Security Vulnerability Scan
    - Code Quality Analysis

### For `develop`:
- Branch: `develop`
- ✅ Require pull request (1 approval)
- ✅ Allow squash merging (recommended)
- ✅ Require status checks:
    - Build and Test
    - Security Vulnerability Scan

### Additional Settings:
**Settings → General → Pull Requests**
- ✅ Allow squash merging
- ❌ Allow merge commits (disable for stable branch)
- ❌ Allow rebase merging (disable for stable branch)

---

## 💻 Workflow

### 1. Create New Feature
```bash
git checkout develop
git pull origin develop
git checkout -b feature/my-new-feature

# Work on code...
git add .
git commit -m "Add new feature"
git push origin feature/my-new-feature

# Create PR to develop
gh pr create --base develop --head feature/my-new-feature
```

### 2. Merge to develop
- GitHub Actions will check the code
- Requires 1 approval
- Merge after successful checks

### 3. Deploy to stable (Production)
```bash
# When ready for release
git checkout develop
git pull origin develop
gh pr create --base stable --head develop --title "Release v1.0.0"

# Requires 2 approvals
# All tests must pass
# Coverage >= 80%
# No security vulnerabilities (CVSS >= 7)
```

---

## ✅ What CI/CD Checks

### On push to feature/* or develop:
- ✅ Code compilation
- ✅ Unit tests
- ✅ Integration tests
- ✅ Code coverage
- ✅ Security vulnerabilities
- ✅ Code quality (SonarCloud)
- ✅ Docker image build (develop only)

### On PR to stable:
- ✅ All tests from develop
- ✅ Coverage >= 80% (mandatory!)
- ✅ No critical vulnerabilities
- ✅ 2 code reviews

---

## 🧪 Local Testing Before Push

```bash
# Run all tests
mvn clean verify -P test-coverage

# Check coverage
mvn jacoco:report
open target/site/jacoco/index.html

# Check security
mvn org.owasp:dependency-check-maven:check
open target/dependency-check-report.html

# Build Docker image
docker build -t finance:test .
docker run -p 8080:8080 finance:test
```

---

## 🐛 Troubleshooting

### Tests fail locally:
```bash
# Clear Maven cache
mvn clean
rm -rf ~/.m2/repository

# Restart
mvn clean install
```

### Docker build fails:
```bash
# Check Java version
java --version  # Should be 21

# Check Dockerfile syntax
docker build --no-cache -t finance:test .
```

### GitHub Actions fails:
1. Check **Actions** tab in GitHub
2. Click on failed job
3. Review logs
4. Fix issue locally
5. Push again

---

## 📊 Monitoring

### View CI/CD status:
- GitHub → **Actions** tab
- Pull Request → **Checks** tab

### Artifacts (test results):
- Test reports: `target/surefire-reports/`
- Coverage: `target/site/jacoco/`
- Security: `target/dependency-check-report.html`

---

## 🎉 Done!

Your project now has:
- ✅ Automatic testing
- ✅ Security scanning
- ✅ Code quality checks
- ✅ Production branch protection (stable)
- ✅ Docker image build

**Every deploy to stable goes through complete validation!**