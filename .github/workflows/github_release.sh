#!/bin/bash

set -o errexit
set -o nounset
set -o pipefail

base_dir="$( cd "$(dirname "$0")/../.." >/dev/null 2>&1 ; pwd -P )"
readonly base_dir
readonly gradle_properties_file="$base_dir/gradle.properties"

project_version=$(awk -F '=' '$1 == "version" { print $2 }' "$gradle_properties_file")
readonly project_version
echo "Read project version '$project_version' from $gradle_properties_file"

if [[ -z "$project_version" ]]; then
    echo "Could not read project version from $gradle_properties_file" >&2
    exit 1
fi

if [[ "$project_version" == *-SNAPSHOT ]]; then
    echo "Release version must not end with -SNAPSHOT: $project_version" >&2
    exit 1
fi

artifact_path=$(find "$base_dir/build/distributions" -maxdepth 1 -type f -name '*.zip' | sort | head -n 1)
readonly artifact_path

if [[ -z "$artifact_path" ]]; then
    echo "Could not find plugin distribution archive in $base_dir/build/distributions" >&2
    exit 1
fi

echo "Calculate sha256sum for file '$artifact_path'"
file_dir="$(dirname "$artifact_path")"
readonly file_dir
file_name="$(basename "$artifact_path")"
readonly file_name
cd "$file_dir"
readonly checksum_file_name="${file_name}.sha256"
sha256sum "$file_name" > "$checksum_file_name"
readonly checksum_file_path="$file_dir/$checksum_file_name"
cd "$base_dir"

readonly changes_file="$base_dir/doc/changes/changes_${project_version}.md"
if [[ ! -f "$changes_file" ]]; then
    echo "Could not find release notes file $changes_file" >&2
    exit 1
fi

readonly title="Release $project_version"
readonly tag="$project_version"
echo "Creating release:"
echo "Git tag      : $tag"
echo "Title        : $title"
echo "Changes file : $changes_file"
echo "Artifact file: $artifact_path"
echo "Checksum file: $checksum_file_path"

release_url=$(gh release create --latest --title "$title" --notes-file "$changes_file" --target main "$tag" \
    "$artifact_path" "$checksum_file_path")
readonly release_url
echo "Release URL: $release_url"
