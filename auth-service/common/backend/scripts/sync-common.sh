#!/bin/bash

#
# Hamkkebu Common Code Sync Script
#
# This script updates the boilerplate submodule to pull the latest common code.
# Use this in your microservices (auth-service, payment-service, etc.)
#
# Usage:
#   ./scripts/sync-common.sh
#

set -e

echo "======================================"
echo " Hamkkebu Common Code Sync"
echo "======================================"

# Check if boilerplate submodule exists
if [ ! -d "boilerplate" ]; then
    echo "❌ Error: boilerplate submodule not found!"
    echo "   Run: git submodule add <boilerplate-repo-url> boilerplate"
    exit 1
fi

echo ""
echo "📦 Current boilerplate commit:"
cd boilerplate
git log -1 --oneline
cd ..

echo ""
echo "🔄 Syncing common code from boilerplate..."
git submodule update --remote boilerplate

echo ""
echo "📦 Updated boilerplate commit:"
cd boilerplate
git log -1 --oneline
cd ..

echo ""
echo "✅ Common code sync completed!"
echo ""
echo "📝 Changes:"
echo "   - backend/common/"
echo "   - frontend/common/"
echo ""
echo "🔧 Next steps:"
echo "   1. Review the changes: git diff"
echo "   2. Test your service: ./gradlew clean build"
echo "   3. Commit the submodule update: git add boilerplate && git commit -m 'Update boilerplate common code'"
echo ""
