# GitHub Branch Protection Rules

## Configuration for `stable` branch (Production)

Navigate to: **Settings → Branches → Add branch protection rule**

### 1. Branch name pattern
```
stable
```

### 2. Protect matching branches

#### Require a pull request before merging
- ✅ Require approvals: **2**
- ✅ Dismiss stale pull request approvals when new commits are pushed
- ✅ Require review from Code Owners

#### Require status checks to pass before merging
- ✅ Require branches to be up to date before merging
- **Required status checks:**
    - `Build and Test`
    - `Security Vulnerability Scan`
    - `Code Quality Analysis`

#### Require conversation resolution before merging
- ✅ Enable

#### Require signed commits
- ✅ Enable (recommended)

#### Require linear history
- ✅ Enable (recommended)

#### Do not allow bypassing the above settings
- ✅ Enable

---

## Configuration for `develop` branch (Integration)

### 1. Branch name pattern
```
develop
```

### 2. Protect matching branches

#### Require a pull request before merging
- ✅ Require approvals: **1**

#### Require status checks to pass before merging
- **Required status checks:**
    - `Build and Test`
    - `Security Vulnerability Scan`

#### Require conversation resolution before merging
- ✅ Enable

---

## Git Flow Strategy

```
feature/* → develop → stable
    ↓          ↓        ↓
  Tests     Tests    Tests
           + Review + Review
                   + Security
                   + Deploy
```

### Workflow:
1. **feature/*** - development of new features
2. **develop** - integration and testing of features
3. **stable** - production code (only through PR from develop)

### Deploy process to stable:
```bash
# 1. Create PR from develop to stable
git checkout develop
git pull origin develop
gh pr create --base stable --head develop --title "Release v1.0.0"

# 2. GitHub Actions will automatically:
#    - Run all tests
#    - Check security vulnerabilities
#    - Analyze code quality
#    - Check coverage (minimum 80%)

# 3. After approval from 2 reviewers - merge
```