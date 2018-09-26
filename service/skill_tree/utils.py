from skill_tree.models import Level, Method, Skill, User, UserMethod, UserSkill


def flush_all_userskill():
    me = User.objects.get(name="me")
    level_f = Level.objects.get(short_name="F")
    skills = Skill.objects.all()
    for skill in skills:
        UserSkill.objects.get_or_create(
            user=me, skill=skill, skill_level=level_f)


def flush_all_usermethod():
    me = User.objects.get(name="me")
    skills = Skill.objects.all()
    for skill in skills:
        method_families = skill.method_family.all()
        for method_family in method_families:
            methods = Method.objects.filter(method_family=method_family)
            for method in methods:
                UserMethod.objects.get_or_create(
                    user=me, skill=skill, method=method)
